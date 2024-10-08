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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ecmwf.common.technical.StreamPlugThread;

/**
 * The Class PTCPSocket.
 */
public class PTCPSocket extends Socket {

    /** The Constant _log. */
    private static final Logger _log = LogManager.getLogger(PTCPSocket.class);

    /** The Constant _debug. */
    private static final boolean _debug = true;

    /** The comm socket. */
    private Socket _commSocket = null;

    /** The id. */
    private String _id = null;

    /** The data sockets. */
    private Socket[] _dataSockets = null;

    /** The number of streams. */
    private int _numberOfStreams = 0;

    /** The out. */
    private OutputStream _out = null;

    /** The out sync. */
    private final Object _outSync = new Object();

    /** The in. */
    private InputStream _in = null;

    /** The in sync. */
    private final Object _inSync = new Object();

    /**
     * Instantiates a new PTCP socket.
     *
     * @param num
     *            the num
     */
    public PTCPSocket(final int num) {
        _numberOfStreams = num;
        _dataSockets = new Socket[num];
    }

    /**
     * Instantiates a new PTCP socket.
     *
     * @param host
     *            the host
     * @param port
     *            the port
     * @param num
     *            the num
     *
     * @throws java.net.UnknownHostException
     *             the unknown host exception
     * @throws java.io.IOException
     *             Signals that an I/O exception has occurred.
     */
    public PTCPSocket(final String host, final int port, final int num) throws UnknownHostException, IOException {
        _numberOfStreams = num;
        _commSocket = new Socket(host, port);
        _init();
    }

    /**
     * Instantiates a new PTCP socket.
     *
     * @param address
     *            the address
     * @param port
     *            the port
     * @param num
     *            the num
     *
     * @throws java.net.UnknownHostException
     *             the unknown host exception
     * @throws java.io.IOException
     *             Signals that an I/O exception has occurred.
     */
    public PTCPSocket(final InetAddress address, final int port, final int num)
            throws UnknownHostException, IOException {
        _numberOfStreams = num;
        _commSocket = new Socket(address, port);
        _init();
    }

    /**
     * Instantiates a new PTCP socket.
     *
     * @param host
     *            the host
     * @param port
     *            the port
     * @param localAddr
     *            the local addr
     * @param localPort
     *            the local port
     * @param num
     *            the num
     *
     * @throws java.net.UnknownHostException
     *             the unknown host exception
     * @throws java.io.IOException
     *             Signals that an I/O exception has occurred.
     */
    public PTCPSocket(final String host, final int port, final InetAddress localAddr, final int localPort,
            final int num) throws UnknownHostException, IOException {
        _numberOfStreams = num;
        _commSocket = new Socket(host, port, localAddr, localPort);
        _init();
    }

    /**
     * Instantiates a new PTCP socket.
     *
     * @param address
     *            the address
     * @param port
     *            the port
     * @param localAddr
     *            the local addr
     * @param localPort
     *            the local port
     * @param num
     *            the num
     *
     * @throws java.net.UnknownHostException
     *             the unknown host exception
     * @throws java.io.IOException
     *             Signals that an I/O exception has occurred.
     */
    public PTCPSocket(final InetAddress address, final int port, final InetAddress localAddr, final int localPort,
            final int num) throws UnknownHostException, IOException {
        _numberOfStreams = num;
        _commSocket = new Socket(address, port, localAddr, localPort);
        _init();
    }

    /**
     * {@inheritDoc}
     *
     * Returns the address to which the socket is connected.
     */
    @Override
    public InetAddress getInetAddress() {
        return _commSocket.getInetAddress();
    }

    /**
     * {@inheritDoc}
     *
     * Returns an input stream for this socket.
     */
    @Override
    public InputStream getInputStream() throws IOException {
        synchronized (_inSync) {
            if (_in == null) {
                final var tinputs = new InputStream[_numberOfStreams];
                for (var i = 0; i < _numberOfStreams; i++) {
                    tinputs[i] = _dataSockets[i].getInputStream();
                }
                _in = new PTCPInputStream(tinputs);
            }
            return _in;
        }
    }

    /**
     * {@inheritDoc}
     *
     * Enable/disable SO_KEEPALIVE.
     */
    @Override
    public void setKeepAlive(final boolean on) throws SocketException {
        if (_commSocket != null) {
            _commSocket.setKeepAlive(on);
        }
        if (_dataSockets != null) {
            for (var i = 0; i < _numberOfStreams; i++) {
                _dataSockets[i].setKeepAlive(on);
            }
        }
    }

    /**
     * {@inheritDoc}
     *
     * Tests if SO_KEEPALIVE is enabled.
     */
    @Override
    public boolean getKeepAlive() throws SocketException {
        return _commSocket.getKeepAlive();
    }

    /**
     * {@inheritDoc}
     *
     * Gets the local address to which the socket is bound.
     */
    @Override
    public InetAddress getLocalAddress() {
        return _commSocket.getLocalAddress();
    }

    /**
     * {@inheritDoc}
     *
     * Returns the local port to which this socket is bound.
     */
    @Override
    public int getLocalPort() {
        return _commSocket.getPort();
    }

    /**
     * {@inheritDoc}
     *
     * Returns an output stream for this socket.
     */
    @Override
    public OutputStream getOutputStream() throws IOException {
        synchronized (_outSync) {
            if (_out == null) {
                final var toutputs = new OutputStream[_numberOfStreams];
                for (var i = 0; i < _numberOfStreams; i++) {
                    toutputs[i] = _dataSockets[i].getOutputStream();
                }
                _out = new PTCPOutputStream(toutputs);
            }
            return _out;
        }
    }

    /**
     * {@inheritDoc}
     *
     * Returns the remote port to which this socket is connected.
     */
    @Override
    public int getPort() {
        return isConnected() ? _commSocket.getPort() : 0;
    }

    /**
     * {@inheritDoc}
     *
     * Sets the SO_RCVBUF option to the specified value for this Socket. The SO_RCVBUF option is used by the platform's
     * networking code as a hint for the size to set the underlying network I/O buffers.
     *
     * Increasing buffer size can increase the performance of network I/O for high-volume connection, while decreasing
     * it can help reduce the backlog of incoming data. For UDP, this sets the maximum size of a packet that may be sent
     * on this Socket.
     *
     * Because SO_RCVBUF is a hint, applications that want to verify what size the buffers were set to should call
     * getReceiveBufferSize().
     */
    @Override
    public void setReceiveBufferSize(final int size) throws SocketException {
        if (size < 0) {
            return;
        }
        if (_debug) {
            _log.debug("setReceiveBufferSize " + size);
        }
        if (_commSocket != null) {
            _commSocket.setReceiveBufferSize(size);
        }
        if (_dataSockets != null) {
            for (var i = 0; i < _numberOfStreams; i++) {
                _dataSockets[i].setReceiveBufferSize(size);
            }
        }
    }

    /**
     * {@inheritDoc}
     *
     * Gets the value of the SO_RCVBUF option for this Socket, that is the buffer size used by the platform for input on
     * this Socket.
     */
    @Override
    public int getReceiveBufferSize() throws SocketException {
        return _commSocket.getReceiveBufferSize();
    }

    /**
     * {@inheritDoc}
     *
     * Sets the SO_SNDBUF option to the specified value for this Socket. The SO_SNDBUF option is used by the platform's
     * networking code as a hint for the size to set the underlying network I/O buffers.
     *
     * Increasing buffer size can increase the performance of network I/O for high-volume connection, while decreasing
     * it can help reduce the backlog of incoming data. For UDP, this sets the maximum size of a packet that may be sent
     * on this Socket.
     *
     * Because SO_SNDBUF is a hint, applications that want to verify what size the buffers were set to should call
     * getSendBufferSize(). Parameters:
     */
    @Override
    public void setSendBufferSize(final int size) throws SocketException {
        if (size < 0) {
            return;
        }
        if (_debug) {
            _log.debug("setSendBufferSize " + size);
        }
        if (_commSocket != null) {
            _commSocket.setSendBufferSize(size);
        }
        if (_dataSockets != null) {
            for (var i = 0; i < _numberOfStreams; i++) {
                _dataSockets[i].setSendBufferSize(size);
            }
        }
    }

    /**
     * {@inheritDoc}
     *
     * Get value of the SO_SNDBUF option for this Socket, that is the buffer size used by the platform for output on
     * this Socket.
     */
    @Override
    public int getSendBufferSize() throws SocketException {
        return _commSocket.getReceiveBufferSize();
    }

    /**
     * {@inheritDoc}
     *
     * Enable/disable SO_LINGER with the specified linger time in seconds. The maximum timeout value is platform
     * specific. The setting only affects socket close.
     */
    @Override
    public void setSoLinger(final boolean on, final int linger) throws SocketException {
        if (_commSocket != null) {
            _commSocket.setSoLinger(on, linger);
        }
        if (_dataSockets != null) {
            for (var i = 0; i < _numberOfStreams; i++) {
                _dataSockets[i].setSoLinger(on, linger);
            }
        }
    }

    /**
     * {@inheritDoc}
     *
     * Returns setting for SO_LINGER. -1 returns implies that the option is disabled. The setting only affects socket
     * close.
     */
    @Override
    public int getSoLinger() throws SocketException {
        return _commSocket.getSoLinger();
    }

    /**
     * {@inheritDoc}
     *
     * Enable/disable SO_TIMEOUT with the specified timeout, in milliseconds. With this option set to a non-zero
     * timeout, a read() call on the InputStream associated with this Socket will block for only this amount of time. If
     * the timeout expires, a java.io.InterruptedIOException is raised, though the Socket is still valid. The option
     * must be enabled prior to entering the blocking operation to have effect. The timeout must be > 0. A timeout of
     * zero is interpreted as an infinite timeout.
     */
    @Override
    public void setSoTimeout(final int timeout) throws SocketException {
        if (_commSocket != null) {
            _commSocket.setSoTimeout(timeout);
        }
        if (_dataSockets != null) {
            for (var i = 0; i < _numberOfStreams; i++) {
                _dataSockets[i].setSoTimeout(timeout);
            }
        }
    }

    /**
     * {@inheritDoc}
     *
     * Returns setting for SO_TIMEOUT. 0 returns implies that the option is disabled (i.e., timeout of infinity).
     */
    @Override
    public int getSoTimeout() throws SocketException {
        return _commSocket.getSoTimeout();
    }

    /**
     * {@inheritDoc}
     *
     * Tests if TCP_NODELAY is enabled.
     */
    @Override
    public boolean getTcpNoDelay() throws SocketException {
        return _commSocket.getTcpNoDelay();
    }

    /**
     * {@inheritDoc}
     *
     * Enable/disable TCP_NODELAY (disable/enable Nagle's algorithm).
     */
    @Override
    public void setTcpNoDelay(final boolean on) throws SocketException {
        if (_commSocket != null) {
            _commSocket.setTcpNoDelay(on);
        }
        if (_dataSockets != null) {
            for (var i = 0; i < _numberOfStreams; i++) {
                _dataSockets[i].setTcpNoDelay(on);
            }
        }
    }

    /**
     * Adds the socket.
     *
     * @param sockets
     *            the sockets
     * @param commSock
     *            the comm sock
     */
    public void addSocket(final Socket[] sockets, final Socket commSock) {
        _commSocket = commSock;
        for (var i = 0; i < _numberOfStreams; i++) {
            if (_dataSockets[i] != null) {
                if (_debug) {
                    _log.warn("Socket " + i + " is a zombie");
                }
                break;
            }
            if (_debug) {
                _log.debug("Socket " + i + " registered");
            }
            _dataSockets[i] = sockets[i];
        }
    }

    /**
     * {@inheritDoc}
     *
     * Places the input stream for this socket at "end of stream". Any data sent to the input stream side of the socket
     * is acknowledged and then silently discarded. If you read from a socket input stream after invoking
     * shutdownInput() on the socket, the stream will return EOF.
     */
    @Override
    public void shutdownInput() throws IOException {
        throw new IOException("Operation not supported");
    }

    /**
     * {@inheritDoc}
     *
     * Disables the output stream for this socket. For a TCP socket, any previously written data will be sent followed
     * by TCP's normal connection termination sequence. If you write to a socket output stream after invoking
     * shutdownOutput() on the socket, the stream will throw an IOException.
     */
    @Override
    public void shutdownOutput() throws IOException {
        throw new IOException("Operation not supported");
    }

    /**
     * {@inheritDoc}
     *
     * Converts this socket to a String.
     */
    @Override
    public String toString() {
        return _commSocket.toString();
    }

    /**
     * {@inheritDoc}
     *
     * Returns the connection state of the socket.
     */
    @Override
    public boolean isConnected() {
        return _commSocket != null && _commSocket.isConnected();
    }

    /**
     * {@inheritDoc}
     *
     * Close.
     */
    @Override
    public void close() throws IOException {
        StreamPlugThread.closeQuietly(_out);
        StreamPlugThread.closeQuietly(_in);
        if (isConnected()) {
            StreamPlugThread.closeQuietly(_commSocket);
            for (var i = 0; i < _numberOfStreams; i++) {
                StreamPlugThread.closeQuietly(_dataSockets[i]);
            }
        }
        super.close();
    }

    /**
     * Initiate contact to the server side.
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    private void _init() throws IOException {
        _id = InetAddress.getLocalHost().getHostAddress() + ":" + String.valueOf(System.currentTimeMillis());
        final var startRtt = System.currentTimeMillis();
        _sendStartRTT(startRtt);
        if (startRtt != _receiveStartRTTEcho(_commSocket)) {
            throw new IOException("Protocol error (inconsistent startRtt)");
        }
        _dataSockets = new Socket[_numberOfStreams];
        for (var i = 0; i < _numberOfStreams; i++) {
            _dataSockets[i] = new Socket();
            _dataSockets[i].connect(_commSocket.getRemoteSocketAddress(), _commSocket.getPort());
            _registerDataSockets(_dataSockets[i], _id);
            if (_debug) {
                _log.debug("Socket " + i + " registered to Server");
            }
        }
        if (_debug) {
            _log.debug("Client completed server negociations");
        }
    }

    /**
     * Send start RTT.
     *
     * @param startRtt
     *            the start rtt
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    private void _sendStartRTT(final long startRtt) throws IOException {
        final var header = new ByteArrayOutputStream(4 + _id.getBytes().length + 4 + 4 + 8);
        final var out = new DataOutputStream(header);
        out.writeInt(_id.getBytes().length);
        out.writeBytes(_id);
        out.writeInt(PTCPServerSocket.PTCP_SOCKET); // Type
        out.writeInt(_numberOfStreams);
        out.writeLong(startRtt);
        final var stream = _commSocket.getOutputStream();
        stream.write(header.toByteArray());
        stream.flush();
    }

    /**
     * Receive start RTT echo.
     *
     * @param commSocket
     *            the comm socket
     *
     * @return the long
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    private static long _receiveStartRTTEcho(final Socket commSocket) throws IOException {
        final var stream = new DataInputStream(commSocket.getInputStream());
        final var header = new byte[8];
        stream.readFully(header);
        final var in = new DataInputStream(new ByteArrayInputStream(header));
        return in.readLong();
    }

    /**
     * Register data sockets.
     *
     * @param socket
     *            the socket
     * @param id
     *            the id
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    private static void _registerDataSockets(final Socket socket, final String id) throws IOException {
        final var header = new ByteArrayOutputStream(4 + id.getBytes().length + 4);
        final var out = new DataOutputStream(header);
        out.writeInt(id.getBytes().length);
        out.writeBytes(id);
        out.writeInt(PTCPServerSocket.DATA_SOCKET);
        final var stream = socket.getOutputStream();
        stream.write(header.toByteArray());
        stream.flush();
    }
}
